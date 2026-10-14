package com.acme.transport.linehaul.backhaul.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictBackhaulMappingTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictBackhaulMapping("code-648", "title-149")
        assertEquals(StrictBackhaulMappingStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newStrictBackhaulMapping("code-648", "title-149")
        try {
            item.moveTo(StrictBackhaulMappingStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictBackhaulMappingStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictBackhaulMapping("code-648", "title-149")
        item.status = StrictBackhaulMappingStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictBackhaulMappingStatus.PLANNED) }
    }
}
