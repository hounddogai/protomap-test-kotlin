package com.acme.transport.safety.coaching.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultCoachingAgreementTest {
    @Test
    fun `starts in planned`() {
        val item = newDefaultCoachingAgreement("code-874", "label-526")
        assertEquals(DefaultCoachingAgreementStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newDefaultCoachingAgreement("code-874", "label-526")
        try {
            item.moveTo(DefaultCoachingAgreementStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultCoachingAgreementStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefaultCoachingAgreement("code-874", "label-526")
        item.status = DefaultCoachingAgreementStatus.CANCELLED
        assertFails("move") { item.moveTo(DefaultCoachingAgreementStatus.PLANNED) }
    }
}
