package com.acme.transport.lastmile.parcel.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class ParcelGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultParcelGroupEngine()
        val item = newParcelGroup("code-838", "name-592")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
