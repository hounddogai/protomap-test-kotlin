package com.acme.transport.terminal.dockdoor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DockDoorForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDockDoorForecastStore()
        val item = newDockDoorForecast("code-771", "name-30")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
