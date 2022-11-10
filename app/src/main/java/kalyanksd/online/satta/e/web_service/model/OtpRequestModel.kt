package kalyanksd.online.satta.e.web_service.model

data class OtpRequestModel(
    val msg: String,
    val otp: Int,
    val status: Boolean

)