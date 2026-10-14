package com.acme.transport.network.node.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalNodeAgreementTest {
    @Test
    fun `starts in created`() {
        val item = newSeasonalNodeAgreement("code-534", "label-371")
        assertEquals(SeasonalNodeAgreementStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newSeasonalNodeAgreement("code-534", "label-371")
        try {
            item.moveTo(SeasonalNodeAgreementStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalNodeAgreementStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSeasonalNodeAgreement("code-534", "label-371")
        item.status = SeasonalNodeAgreementStatus.RETIRED
        assertFails("move") { item.moveTo(SeasonalNodeAgreementStatus.CREATED) }
    }
}
