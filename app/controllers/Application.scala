package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

object Application extends Controller {

    val usernameForm = Form( "username" -> text )

    val hubEnum = Enumerator.imperative[JsValue]( )

    val hub = Concurrent.hub[JsValue]( hubEnum )
  
    def index = Action {
        Ok( views.html.index() )
    }

    def startGame = Action { implicit request =>
        usernameForm.bindFromRequest.fold (
            formWithErrors => BadRequest( "You need to post a 'username' value!" ),
            { maybeFormValue =>
                Redirect( "/game/" + maybeFormValue )
            } 
        )
    }

    def game(name: String) =  Action {
        Ok( views.html.game( name ) )
    }

    def stream( username: String ) = WebSocket.async[JsValue] { request =>
        var out = hub.getPatchCord( )
        var in = Iteratee.foreach[JsValue] ( _ match {
            case message: JsObject => {
                hubEnum.push( message )
            }
        })
        Promise.pure( ( in, out ) )
    }
}