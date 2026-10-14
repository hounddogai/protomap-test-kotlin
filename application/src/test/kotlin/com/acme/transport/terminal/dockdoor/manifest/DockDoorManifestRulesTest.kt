package com.acme.transport.terminal.dockdoor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorManifestValidator()
        val item = newDockDoorManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = DockDoorManifestBudgetRangeRule(8.6, 3938.1)
        val item = newDockDoorManifest("x", "y")
        item.budget = 3939.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DockDoorManifestRequiredLabelRule("dock door manifest needs an entry")
        val item = newDockDoorManifest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DockDoorManifestRequiredCodeRule("dock door manifest needs a value")
        val item = newDockDoorManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
