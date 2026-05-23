package com.masar.portal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.masar.portal.model.MeResponse
import com.masar.portal.ui.components.*
import com.masar.portal.ui.theme.*

@Composable
fun HomeScreen(
    baseUrl: String,
    driverName: String,
    data: MeResponse?,
    loading: Boolean,
) {
    val scroll = rememberScrollState()
    Column(
        Modifier
            .fillMaxSize()
            .background(Ink)
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ===== رأس الصفحة: ترحيب + صورة المندوب =====
        HeaderCard(baseUrl, driverName, data)

        if (loading && data == null) {
            Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandRed)
            }
            return@Column
        }

        // ===== نظرة عامة على الأداء =====
        if (data?.ok == true) {
            PerformanceOverviewCard(data)
            SalaryCard(data)
            QuickStatsRow(data)
        }
    }
}

@Composable
private fun HeaderCard(baseUrl: String, driverName: String, data: MeResponse?) {
    val photo = data?.driver?.driver_photo
    val fullPhoto = if (photo != null && !photo.startsWith("http")) "$baseUrl/$photo" else photo

    GradientCard(
        gradient = listOf(BrandRed, BrandRedDark),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                if (fullPhoto != null) {
                    AsyncImage(
                        model = fullPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.White,
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text("مرحبًا", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(4.dp))
                Text(
                    driverName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                val cid = data?.driver?.courier_id
                if (!cid.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("معرّف: $cid", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
private fun PerformanceOverviewCard(data: MeResponse) {
    val perf = data.perf
    val sup = data.sup
    val orders = data.orders

    MasarCard {
        SectionTitle("نظرة عامة على الأداء")
        Spacer(Modifier.height(14.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CategoryBadge(level = perf?.level_cat ?: "-")
            Column(Modifier.weight(1f)) {
                Text("الفئة الحالية", style = MaterialTheme.typography.labelSmall, color = TxtDim)
                Text(
                    perf?.level_cat ?: "غير مصنّف",
                    style = MaterialTheme.typography.titleMedium,
                    color = TxtPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (perf?.city_rank != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("الترتيب", style = MaterialTheme.typography.labelSmall, color = TxtDim)
                    Text("#${perf.city_rank}", style = MaterialTheme.typography.titleMedium, color = BrandRed, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // التقدم نحو الهدف
        val target = data.salary?.target ?: 600
        val achieved = data.salary?.effective_delivered ?: 0
        ProgressRow(label = "التقدم نحو الهدف ($target طلب)", current = achieved, target = target)

        Spacer(Modifier.height(16.dp))

        // إحصاءات سريعة
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("المسلّمة", "${orders?.deliv ?: 0}", accent = Green, modifier = Modifier.weight(1f))
            StatCard("في الموعد", "${sup?.on_time_tasks ?: 0}", accent = BrandRed, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("الإلغاءات", "${sup?.cancellations ?: 0}",
                accent = if ((sup?.cancellations ?: 0) > 0) Red else TxtDim, modifier = Modifier.weight(1f))
            StatCard("أيام العمل", "${sup?.vda_days ?: 0}",
                accent = if ((sup?.vda_days ?: 0) >= 26) Green else Amber, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SalaryCard(data: MeResponse) {
    val s = data.salary ?: return
    MasarCard {
        SectionTitle("الراتب الحالي")
        Spacer(Modifier.height(12.dp))
        Text(
            text = formatMoney(s.net),
            style = MaterialTheme.typography.displayMedium,
            color = if (s.net >= s.base) Green else Red,
            fontWeight = FontWeight.Bold,
        )
        Text("صافي الراتب المتوقع", style = MaterialTheme.typography.labelSmall, color = TxtDim)

        Spacer(Modifier.height(14.dp))
        Divider(color = LineDim)
        Spacer(Modifier.height(10.dp))

        InfoRow("الراتب الأساسي", formatMoney(s.base))

        if (s.bonuses.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("الإضافات والمكافآت", style = MaterialTheme.typography.labelSmall, color = TxtDim)
            s.bonuses.forEach { b ->
                InfoRow(b.t, "+ ${formatMoney(b.a)}", valueColor = Green)
            }
        }

        if (s.deductions.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("الخصومات", style = MaterialTheme.typography.labelSmall, color = TxtDim)
            s.deductions.forEach { d ->
                InfoRow(d.t, "- ${formatMoney(d.a)}", valueColor = Red)
            }
        }
    }
}

@Composable
private fun QuickStatsRow(data: MeResponse) {
    val orders = data.orders
    MasarCard {
        SectionTitle("ملخّص الطلبات")
        Spacer(Modifier.height(12.dp))
        InfoRow("إجمالي المسلّمة",  "${orders?.deliv ?: 0}")
        InfoRow("الموكّلة",          "${orders?.acc ?: 0}")
        InfoRow("المرفوضة",         "${orders?.rej ?: 0}",
            valueColor = if ((orders?.rej ?: 0) > 0) Red else TxtPrimary)
        InfoRow("المتأخرة",          "${orders?.late_total ?: 0}",
            valueColor = if ((orders?.late_total ?: 0) > 0) Amber else TxtPrimary)
        InfoRow("أيام عمل (تقرير)", "${orders?.days_count ?: 0} يوم")
        if (orders?.first_d != null && orders.last_d != null) {
            InfoRow("الفترة", "${orders.first_d} → ${orders.last_d}")
        }
    }
}

internal fun formatMoney(v: Double): String {
    val rounded = (v * 100).toLong() / 100.0
    return "%,.2f ﷼".format(rounded)
}
