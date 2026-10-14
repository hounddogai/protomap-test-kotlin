package com.acme.transport.lastmile.proofofdelivery.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryProofOfDeliveryNoticeTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryProofOfDeliveryNotice("code-54", "name-773")
        assertEquals(PrimaryProofOfDeliveryNoticeStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPrimaryProofOfDeliveryNotice("code-54", "name-773")
        try {
            item.moveTo(PrimaryProofOfDeliveryNoticeStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryProofOfDeliveryNoticeStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryProofOfDeliveryNotice("code-54", "name-773")
        item.status = PrimaryProofOfDeliveryNoticeStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryProofOfDeliveryNoticeStatus.REQUESTED) }
    }
}
