package com.acme.transport.linehaul.capacity.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCapacityAgreementTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalCapacityAgreement("code-961", "title-717")
        assertEquals(GlobalCapacityAgreementStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newGlobalCapacityAgreement("code-961", "title-717")
        try {
            item.moveTo(GlobalCapacityAgreementStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCapacityAgreementStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalCapacityAgreement("code-961", "title-717")
        item.status = GlobalCapacityAgreementStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalCapacityAgreementStatus.PLANNED) }
    }
}
