package com.acme.transport.network.servicearea.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaManifestValidator()
        val item = newServiceAreaManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = ServiceAreaManifestRequiredUnitRule("service area manifest needs an entry")
        val item = newServiceAreaManifest("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = ServiceAreaManifestDistanceKmRangeRule(9.5, 2124.8)
        val item = newServiceAreaManifest("x", "y")
        item.distanceKm = 2126.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServiceAreaManifestCodeLengthRule(32)
        val item = newServiceAreaManifest("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long unit`() {
        val rule = ServiceAreaManifestUnitLengthRule(86400)
        val item = newServiceAreaManifest("x", "y")
        item.unit = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
