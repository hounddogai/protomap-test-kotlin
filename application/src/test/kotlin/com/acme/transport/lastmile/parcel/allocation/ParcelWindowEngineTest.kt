package com.acme.transport.lastmile.parcel.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class ParcelWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultParcelWindowEngine()
        val item = newParcelWindow("code-421", "name-776")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
