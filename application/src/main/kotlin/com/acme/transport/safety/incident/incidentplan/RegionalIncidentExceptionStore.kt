package com.acme.transport.safety.incident.incidentplan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps regional incident exceptions by code. */
interface RegionalIncidentExceptionStore {
    fun find(code: String): RegionalIncidentException?
    fun save(item: RegionalIncidentException)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalIncidentException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalIncidentExceptionStore(
    var items: MutableMap<String, RegionalIncidentException>,
    var order: MutableList<String>,
) : RegionalIncidentExceptionStore {
    override fun find(code: String): RegionalIncidentException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalIncidentException) {
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

    override fun list(): MutableList<RegionalIncidentException> {
        val out = mutableListOf<RegionalIncidentException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalIncidentExceptionStore(): RegionalIncidentExceptionStore {
    return MemoryRegionalIncidentExceptionStore(
        mutableMapOf<String, RegionalIncidentException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalIncidentExceptionStore(
    var inner: RegionalIncidentExceptionStore,
    var cache: TtlCache<RegionalIncidentException>,
    var hits: Int,
    var misses: Int,
) : RegionalIncidentExceptionStore {
    override fun find(code: String): RegionalIncidentException? {
        val hit = (cache.get(code) as RegionalIncidentException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalIncidentException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalIncidentException> = inner.list()
}

fun newCachedRegionalIncidentExceptionStore(inner: RegionalIncidentExceptionStore, ttl: Int, clock: Clock): RegionalIncidentExceptionStore {
    return CachedRegionalIncidentExceptionStore(
        inner = inner,
        cache = newTtlCache<RegionalIncidentException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional incident exceptions. Empty fields match all. */
data class RegionalIncidentExceptionQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalIncidentException): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalIncidentException>): MutableList<RegionalIncidentException> {
        val matched = filter(items) { candidate: RegionalIncidentException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalIncidentExceptions(store: RegionalIncidentExceptionStore, query: RegionalIncidentExceptionQuery): MutableList<RegionalIncidentException> {
    return query.apply(store.list())
}
