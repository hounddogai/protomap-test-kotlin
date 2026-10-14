package com.acme.transport.fleetops.utilization.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultUtilizationPreferenceTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultUtilizationPreference("code-445", "name-965")
        assertEquals(DefaultUtilizationPreferenceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newDefaultUtilizationPreference("code-445", "name-965")
        try {
            item.moveTo(DefaultUtilizationPreferenceStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultUtilizationPreferenceStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultUtilizationPreference("code-445", "name-965")
        item.status = DefaultUtilizationPreferenceStatus.FAILED
        assertFails("move") { item.moveTo(DefaultUtilizationPreferenceStatus.NEW) }
    }
}
