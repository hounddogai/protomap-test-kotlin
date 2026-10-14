package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulAgreementTest {
    @Test
    fun `starts in requested`() {
        val item = newBackhaulAgreement("code-944", "label-283")
        assertEquals(BackhaulAgreementStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newBackhaulAgreement("code-944", "label-283")
        try {
            item.moveTo(BackhaulAgreementStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulAgreementStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBackhaulAgreement("code-944", "label-283")
        item.status = BackhaulAgreementStatus.EXPIRED
        assertFails("move") { item.moveTo(BackhaulAgreementStatus.REQUESTED) }
    }
}
