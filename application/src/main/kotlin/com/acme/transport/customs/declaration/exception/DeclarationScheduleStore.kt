package com.acme.transport.customs.declaration.exception

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration schedules by code. */
interface DeclarationScheduleStore {
    fun find(code: String): DeclarationSchedule?
    fun save(item: DeclarationSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationScheduleStore(
    var items: MutableMap<String, DeclarationSchedule>,
    var order: MutableList<String>,
) : DeclarationScheduleStore {
    override fun find(code: String): DeclarationSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationSchedule) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<DeclarationSchedule> {
        val out = mutableListOf<DeclarationSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationScheduleStore(): DeclarationScheduleStore {
    return MemoryDeclarationScheduleStore(mutableMapOf<String, DeclarationSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationScheduleStore(
    var inner: DeclarationScheduleStore,
    var cache: TtlCache<DeclarationSchedule>,
    var hits: Int,
    var misses: Int,
) : DeclarationScheduleStore {
    override fun find(code: String): DeclarationSchedule? {
        val hit = (cache.get(code) as DeclarationSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationSchedule> = inner.list()
}

fun newCachedDeclarationScheduleStore(inner: DeclarationScheduleStore, ttl: Int, clock: Clock): DeclarationScheduleStore {
    return CachedDeclarationScheduleStore(
        inner = inner,
        cache = newTtlCache<DeclarationSchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration schedules. Empty fields match all. */
data class DeclarationScheduleQuery(
    var segment: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationSchedule): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationSchedule>): MutableList<DeclarationSchedule> {
        val matched = filter(items) { candidate: DeclarationSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationSchedules(store: DeclarationScheduleStore, query: DeclarationScheduleQuery): MutableList<DeclarationSchedule> {
    return query.apply(store.list())
}
