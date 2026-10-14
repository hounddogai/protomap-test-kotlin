package com.acme.transport.lastmile.parcel.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientParcelTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientParcelTierEngine(newRegistry())
        val item = newLenientParcelTier("code-991", "title-278")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
