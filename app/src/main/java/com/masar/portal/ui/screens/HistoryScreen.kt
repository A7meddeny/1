package com.masar.portal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.masar.portal.model.MeResponse
import com.masar.portal.model.RequestItem
import com.masar.portal.ui.components.MasarCard
import com.masar.portal.ui.components.SectionTitle
import com.masar.portal.ui.theme.*

private val TYPE_LABELS = mapOf(
    "advance"   to "السلف",
    "leave"     to "الإجازات",
    "accident"  to "الحوادث",
    "complaint" to "الشكاوى",
    "fuel"      to "البنزين",
    "report"    to "التقارير",
)

@Composable
fun HistoryScreen(data: MeResponse?) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Ink)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("سجل طلباتي", style = MaterialTheme.typography.headlineSmall, color = TxtPrimary)
        Text(
            "متابعة كل الطلبات السابقة وحالتها لدى الإدارة",
            style = MaterialTheme.typography.bodySmall,
            color = TxtDim,
        )

        val history = data?.history.orEmpty()
        if (history.isEmpty() || history.all { it.value.isEmpty() }) {
            EmptyState()
            return@Column
        }

        TYPE_LABELS.forEach { (key, label) ->
            val items = history[key].orEmpty()
            if (items.isNotEmpty()) {
                MasarCard {
                    SectionTitle("$label (${items.size})")
                    Spacer(Modifier.height(10.dp))
                    items.forEach { req ->
                        RequestRow(req)
                        if (req != items.last()) {
                            Divider(color = LineDim, modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestRow(req: RequestItem) {
    val (badge, badgeBg) = when (req.status) {
        "approved" -> "معتمد"   to Green
        "rejected" -> "مرفوض"   to Red
        else       -> "بانتظار" to Amber
    }
    Column {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeBg.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, badgeBg),
            ) {
                Text(
                    badge,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                    color = badgeBg,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    req.details ?: "(بدون تفاصيل)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TxtPrimary,
                    maxLines = 3,
                )
                if (req.amount != null && req.amount > 0) {
                    Spacer(Modifier.height(3.dp))
                    Text("المبلغ: ${"%,.2f".format(req.amount)} ﷼",
                        style = MaterialTheme.typography.labelSmall, color = Amber, fontWeight = FontWeight.Bold)
                }
                if (!req.review_note.isNullOrBlank()) {
                    Spacer(Modifier.height(5.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Ink3,
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Text("رد الإدارة:", style = MaterialTheme.typography.labelSmall, color = TxtDim)
                            Text(req.review_note, style = MaterialTheme.typography.bodySmall, color = TxtSoft)
                        }
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    (req.created_at ?: "").take(16),
                    style = MaterialTheme.typography.labelSmall,
                    color = TxtDim,
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("📭", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(12.dp))
        Text("لا توجد طلبات سابقة بعد",
            style = MaterialTheme.typography.titleMedium, color = TxtPrimary)
        Spacer(Modifier.height(6.dp))
        Text("اضغط على «الخدمات» لتقديم أول طلب",
            style = MaterialTheme.typography.bodySmall, color = TxtDim)
    }
}
