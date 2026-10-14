package com.acme.transport.emissions.evcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultEvChargeGroupTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultEvChargeGroup("code-11", "title-251")
        assertEquals(DefaultEvChargeGroupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newDefaultEvChargeGroup("code-11", "title-251")
        try {
            item.moveTo(DefaultEvChargeGroupStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultEvChargeGroupStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultEvChargeGroup("code-11", "title-251")
        item.status = DefaultEvChargeGroupStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultEvChargeGroupStatus.CREATED) }
    }
}
