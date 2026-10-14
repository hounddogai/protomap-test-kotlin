package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDropNoticeTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredDropNotice("code-377", "title-804")
        assertEquals(TieredDropNoticeStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newTieredDropNotice("code-377", "title-804")
        try {
            item.moveTo(TieredDropNoticeStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDropNoticeStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredDropNotice("code-377", "title-804")
        item.status = TieredDropNoticeStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredDropNoticeStatus.PROPOSED) }
    }
}
