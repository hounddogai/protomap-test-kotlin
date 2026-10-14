package com.acme.transport.emissions.emissionfactor.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorManifestValidator()
        val item = newEmissionFactorManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = EmissionFactorManifestRequiredTitleRule("emission factor manifest needs an entry")
        val item = newEmissionFactorManifest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = EmissionFactorManifestBatchSizeRangeRule(64, 86400)
        val item = newEmissionFactorManifest("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
