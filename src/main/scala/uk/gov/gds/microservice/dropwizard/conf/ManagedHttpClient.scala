package uk.gov.gds.microservice.dropwizard.conf

import com.yammer.dropwizard.lifecycle.Managed
import com.sun.jersey.api.client.Client

class ManagedHttpClient(httpClient: Client) extends Managed {

  override def start() {
  }

  override def stop() {
    httpClient.destroy()
  }
}

