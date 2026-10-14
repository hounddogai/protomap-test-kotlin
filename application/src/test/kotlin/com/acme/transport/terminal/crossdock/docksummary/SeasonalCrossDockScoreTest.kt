package com.acme.transport.terminal.crossdock.docksummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalCrossDockScoreTest {
    @Test
    fun `starts in created`() {
        val item = newSeasonalCrossDockScore("code-817", "label-279")
        assertEquals(SeasonalCrossDockScoreStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newSeasonalCrossDockScore("code-817", "label-279")
        try {
            item.moveTo(SeasonalCrossDockScoreStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalCrossDockScoreStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSeasonalCrossDockScore("code-817", "label-279")
        item.status = SeasonalCrossDockScoreStatus.RETIRED
        assertFails("move") { item.moveTo(SeasonalCrossDockScoreStatus.CREATED) }
    }
}
