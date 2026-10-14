package com.acme.transport.linehaul.laneplan.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackLanePlanPreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newFallbackLanePlanPreference("code-13", "name-684")
        assertEquals(FallbackLanePlanPreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newFallbackLanePlanPreference("code-13", "name-684")
        try {
            item.moveTo(FallbackLanePlanPreferenceStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackLanePlanPreferenceStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFallbackLanePlanPreference("code-13", "name-684")
        item.status = FallbackLanePlanPreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(FallbackLanePlanPreferenceStatus.REQUESTED) }
    }
}
