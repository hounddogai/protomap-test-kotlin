package com.acme.transport.safety.violation.violationpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveViolationScoreTest {
    @Test
    fun `starts in proposed`() {
        val item = newAdaptiveViolationScore("code-47", "label-863")
        assertEquals(AdaptiveViolationScoreStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newAdaptiveViolationScore("code-47", "label-863")
        try {
            item.moveTo(AdaptiveViolationScoreStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveViolationScoreStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAdaptiveViolationScore("code-47", "label-863")
        item.status = AdaptiveViolationScoreStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AdaptiveViolationScoreStatus.PROPOSED) }
    }
}
