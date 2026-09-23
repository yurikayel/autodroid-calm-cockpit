package com.example.autodroid.domain.usecase

import com.example.autodroid.domain.model.RadioStation
import com.example.autodroid.domain.repository.RadioRepository
import javax.inject.Inject

class GetStationByIdUseCase @Inject constructor(
    private val repository: RadioRepository
) {
    suspend operator fun invoke(id: String): Result<RadioStation?> = repository.getStation(id)
}
