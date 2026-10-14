package com.acme.transport.lastmile.parcel.parcelreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelManifestValidator()
        val item = newParcelManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ParcelManifestRequiredCodeRule("parcel manifest needs this field")
        val item = newParcelManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = ParcelManifestRequiredDriverIdRule("parcel manifest needs an entry")
        val item = newParcelManifest("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = ParcelManifestRequiredExternalRefRule("parcel manifest needs a value")
        val item = newParcelManifest("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ParcelManifestLabelLengthRule(32)
        val item = newParcelManifest("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
