package com.acme.transport.lastmile.parcel.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyParcelScoreTest {
    @Test
    fun `starts in created`() {
        val item = newLegacyParcelScore("code-815", "title-497")
        assertEquals(LegacyParcelScoreStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLegacyParcelScore("code-815", "title-497")
        try {
            item.moveTo(LegacyParcelScoreStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyParcelScoreStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLegacyParcelScore("code-815", "title-497")
        item.status = LegacyParcelScoreStatus.RETIRED
        assertFails("move") { item.moveTo(LegacyParcelScoreStatus.CREATED) }
    }
}
