package com.acme.transport.safety.incident.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentBaselineTest {
    @Test
    fun `starts in proposed`() {
        val item = newIncidentBaseline("code-918", "label-640")
        assertEquals(IncidentBaselineStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newIncidentBaseline("code-918", "label-640")
        try {
            item.moveTo(IncidentBaselineStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentBaselineStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newIncidentBaseline("code-918", "label-640")
        item.status = IncidentBaselineStatus.WITHDRAWN
        assertFails("move") { item.moveTo(IncidentBaselineStatus.PROPOSED) }
    }
}
