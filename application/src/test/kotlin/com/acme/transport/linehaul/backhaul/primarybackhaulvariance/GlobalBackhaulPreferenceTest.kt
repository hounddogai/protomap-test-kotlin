package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalBackhaulPreferenceTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalBackhaulPreference("code-704", "label-235")
        assertEquals(GlobalBackhaulPreferenceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newGlobalBackhaulPreference("code-704", "label-235")
        try {
            item.moveTo(GlobalBackhaulPreferenceStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalBackhaulPreferenceStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalBackhaulPreference("code-704", "label-235")
        item.status = GlobalBackhaulPreferenceStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalBackhaulPreferenceStatus.PLANNED) }
    }
}
