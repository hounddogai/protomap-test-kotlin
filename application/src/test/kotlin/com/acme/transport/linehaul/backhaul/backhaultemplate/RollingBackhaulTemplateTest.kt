package com.acme.transport.linehaul.backhaul.backhaultemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingBackhaulTemplateTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingBackhaulTemplate("code-26", "name-803")
        assertEquals(RollingBackhaulTemplateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRollingBackhaulTemplate("code-26", "name-803")
        try {
            item.moveTo(RollingBackhaulTemplateStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingBackhaulTemplateStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingBackhaulTemplate("code-26", "name-803")
        item.status = RollingBackhaulTemplateStatus.STOPPED
        assertFails("move") { item.moveTo(RollingBackhaulTemplateStatus.IDLE) }
    }
}
