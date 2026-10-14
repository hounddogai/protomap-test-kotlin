package com.acme.transport.linehaul.capacity.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityAgreementTest {
    @Test
    fun `starts in new`() {
        val item = newCapacityAgreement("code-940", "title-529")
        assertEquals(CapacityAgreementStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCapacityAgreement("code-940", "title-529")
        try {
            item.moveTo(CapacityAgreementStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityAgreementStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCapacityAgreement("code-940", "title-529")
        item.status = CapacityAgreementStatus.FAILED
        assertFails("move") { item.moveTo(CapacityAgreementStatus.NEW) }
    }
}
