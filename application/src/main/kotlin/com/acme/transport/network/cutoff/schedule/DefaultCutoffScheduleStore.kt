package com.acme.transport.network.cutoff.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default cutoff schedules by code. */
interface DefaultCutoffScheduleStore {
    fun find(code: String): DefaultCutoffSchedule?
    fun save(item: DefaultCutoffSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCutoffSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCutoffScheduleStore(
    var items: MutableMap<String, DefaultCutoffSchedule>,
    var order: MutableList<String>,
) : DefaultCutoffScheduleStore {
    override fun find(code: String): DefaultCutoffSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCutoffSchedule) {
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

    override fun list(): MutableList<DefaultCutoffSchedule> {
        val out = mutableListOf<DefaultCutoffSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCutoffScheduleStore(): DefaultCutoffScheduleStore {
    return MemoryDefaultCutoffScheduleStore(
        mutableMapOf<String, DefaultCutoffSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCutoffScheduleStore(
    var inner: DefaultCutoffScheduleStore,
    var cache: TtlCache<DefaultCutoffSchedule>,
    var hits: Int,
    var misses: Int,
) : DefaultCutoffScheduleStore {
    override fun find(code: String): DefaultCutoffSchedule? {
        val hit = (cache.get(code) as DefaultCutoffSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCutoffSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCutoffSchedule> = inner.list()
}

fun newCachedDefaultCutoffScheduleStore(inner: DefaultCutoffScheduleStore, ttl: Int, clock: Clock): DefaultCutoffScheduleStore {
    return CachedDefaultCutoffScheduleStore(
        inner = inner,
        cache = newTtlCache<DefaultCutoffSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default cutoff schedules. Empty fields match all. */
data class DefaultCutoffScheduleQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCutoffSchedule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultCutoffSchedule>): MutableList<DefaultCutoffSchedule> {
        val matched = filter(items) { candidate: DefaultCutoffSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCutoffSchedules(store: DefaultCutoffScheduleStore, query: DefaultCutoffScheduleQuery): MutableList<DefaultCutoffSchedule> {
    return query.apply(store.list())
}
