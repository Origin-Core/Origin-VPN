package com.origin.vpn.domain.usecase

import com.origin.vpn.domain.repository.VpnRepository
import javax.inject.Inject

class DisconnectVpnUseCase @Inject constructor(
    private val repository: VpnRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            repository.disconnect()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
