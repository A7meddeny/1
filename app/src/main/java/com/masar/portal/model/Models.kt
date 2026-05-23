package com.masar.portal.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class LoginResponse(
    val ok: Boolean,
    val token: String? = null,
    val driver: LoginDriver? = null,
    val error: String? = null,
)

@Serializable
data class LoginDriver(
    val id: Int,
    val name: String,
    val courier_id: String,
    val national_id: String,
)

@Serializable
data class MeResponse(
    val ok: Boolean,
    val driver: DriverInfo? = null,
    val orders: OrdersSummary? = null,
    val perf: PerfInfo? = null,
    val sup: SupInfo? = null,
    val history: Map<String, List<RequestItem>> = emptyMap(),
    val salary: SalaryInfo? = null,
    val error: String? = null,
)

@Serializable
data class DriverInfo(
    val id: Int? = null,
    val full_name: String? = null,
    val courier_id: String? = null,
    val national_id: String? = null,
    val phone: String? = null,
    val plate_letters: String? = null,
    val plate_numbers: String? = null,
    val bank_account: String? = null,
    val iban: String? = null,
    val driver_photo: String? = null,
    val car_photo: String? = null,
    val iqama_photo: String? = null,
    val driver_card_expiry: String? = null,
    val car_op_expiry: String? = null,
    val iqama_next_expiry: String? = null,
    val car_auth_expiry: String? = null,
)

@Serializable
data class OrdersSummary(
    val deliv: Int? = null,
    val acc: Int? = null,
    val rej: Int? = null,
    val late_total: Int? = null,
    val days_count: Int? = null,
    val first_d: String? = null,
    val last_d: String? = null,
)

@Serializable
data class PerfInfo(
    val level_cat: String? = null,
    val city_rank: Int? = null,
    val on_time_rate: Double? = null,
)

@Serializable
data class SupInfo(
    val vda_days: Int? = null,
    val cancellations: Int? = null,
    val total_distance: Double? = null,
    val payable_distance: Double? = null,
    val on_time_tasks: Int? = null,
)

@Serializable
data class RequestItem(
    val id: Int,
    val type: String,
    val details: String? = null,
    val amount: Double? = null,
    val status: String,
    val review_note: String? = null,
    val created_at: String? = null,
    val reviewed_at: String? = null,
)

@Serializable
data class SalaryInfo(
    val base: Double,
    val net: Double,
    val deductions: List<SalaryLine> = emptyList(),
    val bonuses: List<SalaryLine> = emptyList(),
    val effective_delivered: Int,
    val target: Int,
)

@Serializable
data class SalaryLine(
    val t: String,
    val a: Double,
)

@Serializable
data class BrandResponse(
    val ok: Boolean,
    val brand: BrandInfo? = null,
)

@Serializable
data class BrandInfo(
    val app_name: String = "مسار - المناديب",
    val short: String = "مسار",
    val logo: String = "assets/logo.png",
    val primary: String = "#f04d45",
    val track_enabled: Int = 0,
)

@Serializable
data class SimpleResponse(
    val ok: Boolean,
    val error: String? = null,
    val message: String? = null,
)
