package com.acme.transport.linehaul.capacity.capacitypreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCapacityPreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalCapacityPreference("code-994", "name-232")
        assertEquals(RegionalCapacityPreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRegionalCapacityPreference("code-994", "name-232")
        try {
            item.moveTo(RegionalCapacityPreferenceStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCapacityPreferenceStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalCapacityPreference("code-994", "name-232")
        item.status = RegionalCapacityPreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalCapacityPreferenceStatus.REQUESTED) }
    }
}
