package com.acme.transport.lastmile.locker.lockerassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveLockerAgreementTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveLockerAgreement("code-710", "name-889")
        assertEquals(AdaptiveLockerAgreementStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAdaptiveLockerAgreement("code-710", "name-889")
        try {
            item.moveTo(AdaptiveLockerAgreementStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveLockerAgreementStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveLockerAgreement("code-710", "name-889")
        item.status = AdaptiveLockerAgreementStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveLockerAgreementStatus.CREATED) }
    }
}
