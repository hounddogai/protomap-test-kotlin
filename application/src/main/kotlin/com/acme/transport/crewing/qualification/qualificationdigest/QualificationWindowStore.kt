package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification windows by code. */
interface QualificationWindowStore {
    fun find(code: String): QualificationWindow?
    fun save(item: QualificationWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationWindowStore(
    var items: MutableMap<String, QualificationWindow>,
    var order: MutableList<String>,
) : QualificationWindowStore {
    override fun find(code: String): QualificationWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationWindow) {
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

    override fun list(): MutableList<QualificationWindow> {
        val out = mutableListOf<QualificationWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationWindowStore(): QualificationWindowStore {
    return MemoryQualificationWindowStore(mutableMapOf<String, QualificationWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationWindowStore(
    var inner: QualificationWindowStore,
    var cache: TtlCache<QualificationWindow>,
    var hits: Int,
    var misses: Int,
) : QualificationWindowStore {
    override fun find(code: String): QualificationWindow? {
        val hit = (cache.get(code) as QualificationWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationWindow> = inner.list()
}

fun newCachedQualificationWindowStore(inner: QualificationWindowStore, ttl: Int, clock: Clock): QualificationWindowStore {
    return CachedQualificationWindowStore(
        inner = inner,
        cache = newTtlCache<QualificationWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification windows. Empty fields match all. */
data class QualificationWindowQuery(
    var unit: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationWindow): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationWindow>): MutableList<QualificationWindow> {
        val matched = filter(items) { candidate: QualificationWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationWindows(store: QualificationWindowStore, query: QualificationWindowQuery): MutableList<QualificationWindow> {
    return query.apply(store.list())
}
