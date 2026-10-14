package com.acme.transport.safety.incident.incidentsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryIncidentOverrideTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryIncidentOverride("code-820", "title-318")
        assertEquals(PrimaryIncidentOverrideStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newPrimaryIncidentOverride("code-820", "title-318")
        try {
            item.moveTo(PrimaryIncidentOverrideStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryIncidentOverrideStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryIncidentOverride("code-820", "title-318")
        item.status = PrimaryIncidentOverrideStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryIncidentOverrideStatus.PROPOSED) }
    }
}
