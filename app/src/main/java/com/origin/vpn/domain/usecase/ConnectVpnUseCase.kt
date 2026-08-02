package com.origin.vpn.domain.usecase

import com.origin.vpn.data.remote.model.VpnConfig
import com.origin.vpn.domain.repository.VpnRepository
import javax.inject.Inject

class ConnectVpnUseCase @Inject constructor(
    private val repository: VpnRepository
) {
    suspend operator fun invoke(config: VpnConfig): Result<Unit> {
        return try {
            repository.connect(config)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
