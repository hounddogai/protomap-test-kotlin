package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualProofOfDeliveryRecordTest {
    @Test
    fun `starts in requested`() {
        val item = newManualProofOfDeliveryRecord("code-429", "title-870")
        assertEquals(ManualProofOfDeliveryRecordStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newManualProofOfDeliveryRecord("code-429", "title-870")
        try {
            item.moveTo(ManualProofOfDeliveryRecordStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualProofOfDeliveryRecordStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualProofOfDeliveryRecord("code-429", "title-870")
        item.status = ManualProofOfDeliveryRecordStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualProofOfDeliveryRecordStatus.REQUESTED) }
    }
}
