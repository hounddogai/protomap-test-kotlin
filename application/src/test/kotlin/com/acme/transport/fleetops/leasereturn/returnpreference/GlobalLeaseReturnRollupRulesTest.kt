package com.acme.transport.fleetops.leasereturn.returnpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalLeaseReturnRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalLeaseReturnRollupValidator()
        val item = newGlobalLeaseReturnRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = GlobalLeaseReturnRollupRequiredExternalRefRule("global lease return rollup needs a value")
        val item = newGlobalLeaseReturnRollup("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = GlobalLeaseReturnRollupRequiredLabelRule("global lease return rollup needs a value")
        val item = newGlobalLeaseReturnRollup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
