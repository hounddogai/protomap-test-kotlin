package com.acme.transport.lastmile.proofofdelivery.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalProofOfDeliveryRecordTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalProofOfDeliveryRecord("code-657", "title-20")
        assertEquals(GlobalProofOfDeliveryRecordStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newGlobalProofOfDeliveryRecord("code-657", "title-20")
        try {
            item.moveTo(GlobalProofOfDeliveryRecordStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalProofOfDeliveryRecordStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalProofOfDeliveryRecord("code-657", "title-20")
        item.status = GlobalProofOfDeliveryRecordStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalProofOfDeliveryRecordStatus.CREATED) }
    }
}
