package com.acme.transport.emissions.fuelburn.burnwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingFuelBurnAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingFuelBurnAgreementStore()
        val item = newRollingFuelBurnAgreement("code-557", "label-853")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
