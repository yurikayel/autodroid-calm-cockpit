package com.example.autodroid.domain.usecase

import com.example.autodroid.domain.model.RadioStation
import com.example.autodroid.domain.repository.RadioRepository
import javax.inject.Inject

class GetRadioStationsUseCase @Inject constructor(
    private val repository: RadioRepository
) {
    suspend operator fun invoke(): Result<List<RadioStation>> =
        repository.getStations().map { stations -> stations.sortedBy { it.name } }
}
