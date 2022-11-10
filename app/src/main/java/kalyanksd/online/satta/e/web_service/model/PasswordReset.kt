package kalyanksd.online.satta.e.web_service.model

data class PasswordReset(
    val mobile: String,
    val password: String,
    val confirmPassword: String
)
