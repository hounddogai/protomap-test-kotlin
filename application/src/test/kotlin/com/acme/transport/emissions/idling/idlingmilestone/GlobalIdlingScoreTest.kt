package com.acme.transport.emissions.idling.idlingmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalIdlingScoreTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalIdlingScore("code-559", "name-984")
        assertEquals(GlobalIdlingScoreStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newGlobalIdlingScore("code-559", "name-984")
        try {
            item.moveTo(GlobalIdlingScoreStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalIdlingScoreStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalIdlingScore("code-559", "name-984")
        item.status = GlobalIdlingScoreStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalIdlingScoreStatus.CREATED) }
    }
}
