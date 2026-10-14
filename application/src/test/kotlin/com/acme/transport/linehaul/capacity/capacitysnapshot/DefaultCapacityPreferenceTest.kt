package com.acme.transport.linehaul.capacity.capacitysnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultCapacityPreferenceTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultCapacityPreference("code-766", "title-981")
        assertEquals(DefaultCapacityPreferenceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newDefaultCapacityPreference("code-766", "title-981")
        try {
            item.moveTo(DefaultCapacityPreferenceStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultCapacityPreferenceStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultCapacityPreference("code-766", "title-981")
        item.status = DefaultCapacityPreferenceStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultCapacityPreferenceStatus.CREATED) }
    }
}
