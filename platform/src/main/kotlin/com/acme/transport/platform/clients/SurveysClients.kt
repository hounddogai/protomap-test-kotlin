package com.acme.transport.platform.clients

import com.acme.surveys.v1.SurveyQueryServiceGrpcKt
import com.acme.surveys.v1.SurveyServiceGrpcKt
import com.acme.transport.platform.ClientRegistry
import io.grpc.ManagedChannel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin adapters for the surveys API share the mesh channel. */
val surveysClients = module {
    single { SurveyQueryServiceGrpcKt.SurveyQueryServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.surveys.v1.SurveyQueryService", it)
    } }
    single { SurveyServiceGrpcKt.SurveyServiceCoroutineStub(get<ManagedChannel>(named("mesh"))).also {
        ClientRegistry.register("acme.surveys.v1.SurveyService", it)
    } }
}
