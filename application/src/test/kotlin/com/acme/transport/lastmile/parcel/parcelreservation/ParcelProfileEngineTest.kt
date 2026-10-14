package com.acme.transport.lastmile.parcel.parcelreservation

import kotlin.test.Test
import kotlin.test.assertTrue

class ParcelProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultParcelProfileEngine()
        val item = newParcelProfile("code-179", "name-263")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
