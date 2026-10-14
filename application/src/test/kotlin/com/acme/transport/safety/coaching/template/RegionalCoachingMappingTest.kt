package com.acme.transport.safety.coaching.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCoachingMappingTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalCoachingMapping("code-7", "title-808")
        assertEquals(RegionalCoachingMappingStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRegionalCoachingMapping("code-7", "title-808")
        try {
            item.moveTo(RegionalCoachingMappingStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCoachingMappingStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalCoachingMapping("code-7", "title-808")
        item.status = RegionalCoachingMappingStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalCoachingMappingStatus.CREATED) }
    }
}
