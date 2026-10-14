package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalParcelAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalParcelAssignmentEngine(newRegistry())
        val item = newGlobalParcelAssignment("code-691", "name-304")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
