package com.acme.transport.safety.incident.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps manual incident exceptions by code. */
interface ManualIncidentExceptionStore {
    fun find(code: String): ManualIncidentException?
    fun save(item: ManualIncidentException)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualIncidentException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualIncidentExceptionStore(
    var items: MutableMap<String, ManualIncidentException>,
    var order: MutableList<String>,
) : ManualIncidentExceptionStore {
    override fun find(code: String): ManualIncidentException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualIncidentException) {
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

    override fun list(): MutableList<ManualIncidentException> {
        val out = mutableListOf<ManualIncidentException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualIncidentExceptionStore(): ManualIncidentExceptionStore {
    return MemoryManualIncidentExceptionStore(
        mutableMapOf<String, ManualIncidentException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualIncidentExceptionStore(
    var inner: ManualIncidentExceptionStore,
    var cache: TtlCache<ManualIncidentException>,
    var hits: Int,
    var misses: Int,
) : ManualIncidentExceptionStore {
    override fun find(code: String): ManualIncidentException? {
        val hit = (cache.get(code) as ManualIncidentException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualIncidentException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualIncidentException> = inner.list()
}

fun newCachedManualIncidentExceptionStore(inner: ManualIncidentExceptionStore, ttl: Int, clock: Clock): ManualIncidentExceptionStore {
    return CachedManualIncidentExceptionStore(
        inner = inner,
        cache = newTtlCache<ManualIncidentException>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual incident exceptions. Empty fields match all. */
data class ManualIncidentExceptionQuery(
    var title: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualIncidentException): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualIncidentException>): MutableList<ManualIncidentException> {
        val matched = filter(items) { candidate: ManualIncidentException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualIncidentExceptions(store: ManualIncidentExceptionStore, query: ManualIncidentExceptionQuery): MutableList<ManualIncidentException> {
    return query.apply(store.list())
}
