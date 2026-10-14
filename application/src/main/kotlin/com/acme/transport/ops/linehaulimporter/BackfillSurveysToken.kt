package com.acme.transport.ops.linehaulimporter

import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.normalizeAccessPolicyRequest
import com.acme.fleet.v1.InspectionValidationServiceGrpcKt
import com.acme.fleet.v1.getInspectionRulesRequest
import com.acme.fuel.v1.CommentFuelPolicyApprovalRequestKt
import com.acme.fuel.v1.FuelPolicyApprovalServiceGrpcKt
import com.acme.fuel.v1.commentFuelPolicyApprovalRequest
import com.acme.routing.v1.RouteQueryServiceGrpcKt
import com.acme.routing.v1.suggestRoutesRequest
import com.acme.surveys.v1.SurveyQueryServiceGrpcKt
import com.acme.surveys.v1.resolveSurveysRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.surveys.v1.SurveyQueryService/ResolveSurveys and hands what it keeps to the sink of its kind. */
suspend fun backfillSurveysToken(channel: ManagedChannel) {
    val origin = SurveyQueryServiceGrpcKt.SurveyQueryServiceCoroutineStub(channel)
    val got = origin.resolveSurveys(resolveSurveysRequest { })
    val entry = BackfillSurveysTokenRecord(
        nextPageToken = normalize(got.pageInfo.nextPageToken),
        message = got.result.message,
        correlationId = got.result.correlationId,
        status = got.result.status,
    )
    dispatchBackfillSurveysToken("fuel-policy-approval", entry, channel)
}

/** Where backfillSurveysToken hands each record. */
interface BackfillSurveysTokenSink {
    suspend fun hand(rec: BackfillSurveysTokenRecord)
}

/** What backfillSurveysToken keeps of each ResolveSurveys response. */
data class BackfillSurveysTokenRecord(val nextPageToken: String, val message: String, val correlationId: String, val status: String)

/** Sends records to acme.routing.v1.RouteQueryService. */
class BackfillSurveysTokenRouteQuery(private val stub: RouteQueryServiceGrpcKt.RouteQueryServiceCoroutineStub) : BackfillSurveysTokenSink {
    override suspend fun hand(rec: BackfillSurveysTokenRecord) {
        stub.suggestRoutes(suggestRoutesRequest { this.orderBy = rec.nextPageToken })
    }
}

/** Hands records to acme.authorization.v1.AccessPolicyValidationService. */
class BackfillSurveysTokenAccessPolicyValidation(private val stub: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub) : BackfillSurveysTokenSink {
    override suspend fun hand(rec: BackfillSurveysTokenRecord) {
        val request = normalizeAccessPolicyRequest {
            this.accessPolicy = accessPolicy { this.accessPolicyId = rec.status }
            this.accessPolicyId = rec.message
        }
        RpcBoundary.run("acme.authorization.v1.AccessPolicyValidationService/NormalizeAccessPolicy") { stub.normalizeAccessPolicy(request) }
    }
}

/** Sends records to acme.fleet.v1.InspectionValidationService. */
class BackfillSurveysTokenInspectionValidation(private val stub: InspectionValidationServiceGrpcKt.InspectionValidationServiceCoroutineStub) : BackfillSurveysTokenSink {
    override suspend fun hand(rec: BackfillSurveysTokenRecord) {
        RpcBoundary.run("acme.fleet.v1.InspectionValidationService/GetInspectionRules") { stub.getInspectionRules(getInspectionRulesRequest { this.inspectionId = rec.correlationId }) }
    }
}

/** Writes records to acme.fuel.v1.FuelPolicyApprovalService. */
class BackfillSurveysTokenFuelPolicyApproval(private val stub: FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub) : BackfillSurveysTokenSink {
    override suspend fun hand(rec: BackfillSurveysTokenRecord) {
        val request = commentFuelPolicyApprovalRequest {
            this.details = CommentFuelPolicyApprovalRequestKt.details { this.approverEmail = rec.status }
        }
        stub.commentFuelPolicyApproval(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class BackfillSurveysTokenLogOnly : BackfillSurveysTokenSink {
    override suspend fun hand(rec: BackfillSurveysTokenRecord) {
        LoggerFactory.getLogger("BackfillSurveysTokenRecord").atInfo().addKeyValue("nextPageToken", rec.nextPageToken).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchBackfillSurveysToken(kind: String, rec: BackfillSurveysTokenRecord, channel: ManagedChannel) {
    var write: suspend (BackfillSurveysTokenRecord) -> Unit = BackfillSurveysTokenLogOnly()::hand
    when (kind) {
        "route-query" -> write = BackfillSurveysTokenRouteQuery(RouteQueryServiceGrpcKt.RouteQueryServiceCoroutineStub(channel))::hand
        "access-policy-validation" -> write = BackfillSurveysTokenAccessPolicyValidation(AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub(channel))::hand
        "inspection-validation" -> write = BackfillSurveysTokenInspectionValidation(InspectionValidationServiceGrpcKt.InspectionValidationServiceCoroutineStub(channel))::hand
        "fuel-policy-approval" -> write = BackfillSurveysTokenFuelPolicyApproval(FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub(channel))::hand
    }
    write(rec)
}
