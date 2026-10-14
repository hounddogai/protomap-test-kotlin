package com.acme.transport.lastmile.drop.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDropTemplateTest {
    @Test
    fun `starts in created`() {
        val item = newTieredDropTemplate("code-226", "name-144")
        assertEquals(TieredDropTemplateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTieredDropTemplate("code-226", "name-144")
        try {
            item.moveTo(TieredDropTemplateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDropTemplateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredDropTemplate("code-226", "name-144")
        item.status = TieredDropTemplateStatus.RETIRED
        assertFails("move") { item.moveTo(TieredDropTemplateStatus.CREATED) }
    }
}
