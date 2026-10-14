package com.acme.transport.crewing.restbreak.breakmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyRestBreakAgreementTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyRestBreakAgreement("code-13", "label-980")
        assertEquals(LegacyRestBreakAgreementStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLegacyRestBreakAgreement("code-13", "label-980")
        try {
            item.moveTo(LegacyRestBreakAgreementStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyRestBreakAgreementStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyRestBreakAgreement("code-13", "label-980")
        item.status = LegacyRestBreakAgreementStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyRestBreakAgreementStatus.PROPOSED) }
    }
}
