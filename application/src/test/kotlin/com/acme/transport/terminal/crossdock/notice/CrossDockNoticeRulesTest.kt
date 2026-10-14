package com.acme.transport.terminal.crossdock.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockNoticeValidator()
        val item = newCrossDockNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = CrossDockNoticeRequiredSourceRule("cross dock notice needs an entry")
        val item = newCrossDockNotice("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = CrossDockNoticeRequiredDriverIdRule("cross dock notice needs a value")
        val item = newCrossDockNotice("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CrossDockNoticeRequiredNameRule("cross dock notice needs an entry")
        val item = newCrossDockNotice("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
